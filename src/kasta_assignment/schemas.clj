(ns kasta-assignment.schemas
  (:require [schema.core :as s]))

(def create-filter-req-body
  {:topic s/Str
   :q s/Str})

(def delete-filter-req-body
  {:id s/Str})