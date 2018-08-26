(ns kasta-assignment.handler
  (:require [clojure.tools.logging :as log]
            [clojure.string :as string]
            [compojure.core :refer :all]
            [compojure.route :as route]
            [ring.middleware.defaults :refer [wrap-defaults api-defaults]]
            [ring.middleware.json :refer [wrap-json-body wrap-json-response]]
            [ring.util.response :as response]
            [schema.core :as s]

            [kasta-assignment.helpers :as h]
            [kasta-assignment.schemas :as schemas]
            [kasta-assignment.consumer :refer [create-consumer-thread]]))

(defonce db (atom {:filters {}
                   :messages {}
                   :threads {}}))

(defn create-consume-fn [db filter]
  (fn [record]
    (let [value (string/lower-case (.value record))
          q (string/lower-case (:q filter))
          id (:id filter)]
      (when (string/includes? value q)
        (swap! db update-in [:messages id] conj (h/record->data record))))))

(defn create-filter [db req]
  (log/info "Create filter request" req)
  (s/validate schemas/create-filter-req-body (:body req))
  (let [{body :body} req
        id (h/next-id)
        filter (merge body {:id id})
        consume-fn (create-consume-fn db filter)
        consumer-thread (create-consumer-thread
                         consume-fn
                         (:topic body))]
    (swap! db assoc-in [:filters id] filter)
    (swap! db assoc-in [:messages id] [])
    (swap! db assoc-in [:threads id] consumer-thread)
    (response/created "" filter)))

(defn get-messages [db str-id]
  (let [id (keyword str-id)
        filter (get-in @db [:filters id])
        messages (get-in @db [:messages id])]
    (if (nil? filter)
      (response/not-found nil)
      (response/response {:id id
                          :messages (get-in @db [:messages id] [])}))))

(defn get-filters [db req]
  (log/info "Get filters request" req)
  (let [id (h/params-id req)]
    (if id
      (get-messages db id)
      (response/response {:filters (or (vals (:filters @db)) [])}))))

(defn cleanup-filter [db id]
  ; TODO: cleanup consumer properly on removing filter
  (swap! db update :filters dissoc id)
  (swap! db update :threads dissoc id)
  (swap! db update :messages dissoc id))

(defn delete-filter [db req]
  (log/info "Remove filter request" req)
  (s/validate schemas/delete-filter-req-body (:body req))
  (let [id (h/body-id req)
        exists? (get-in @db [:filters id])]
    (if exists?
      (do
        (cleanup-filter db id)
        (response/response nil))
      (response/not-found nil))))

(defroutes app-routes
  (GET "/filter" [] (partial get-filters db))
  (POST "/filter" [] (partial create-filter db))
  (DELETE "/filter" [] (partial delete-filter db))
  (route/not-found "Not Found"))

(def app
  (-> app-routes
      (wrap-defaults api-defaults)
      (wrap-json-body {:keywords? true})
      wrap-json-response))
