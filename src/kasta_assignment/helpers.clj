(ns kasta-assignment.helpers)

(defn next-id []
  (-> (java.util.UUID/randomUUID)
      str
      keyword))

(defn params-id [req]
  (keyword (get-in req [:params :id])))

(defn body-id [req]
  (keyword (get-in req [:body :id])))

(defn record->data [record]
  {:timestamp (.timestamp record)
   :value (.value record)
   :topic (.topic record)
   :partition (.partition record)
   :offset (.offset record)})