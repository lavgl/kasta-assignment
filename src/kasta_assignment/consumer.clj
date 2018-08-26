(ns kasta-assignment.consumer
  (:require [kasta-assignment.helpers :as h])
  (:import org.apache.kafka.clients.consumer.KafkaConsumer
           org.apache.kafka.common.TopicPartition
           org.apache.kafka.clients.consumer.OffsetAndMetadata))

(defn make-config [] {"bootstrap.servers" "localhost:9092"
              "group.id" (str (h/next-id))
              "enable.auto.commit" "false"
              "key.deserializer" "org.apache.kafka.common.serialization.StringDeserializer"
              "value.deserializer" "org.apache.kafka.common.serialization.StringDeserializer"})

(def min-batch-size 10)

(defn lazy-consumer [consumer]
  (lazy-seq
    (let [records (.poll consumer 100)]
      (concat records (lazy-consumer consumer)))))

(defn commit-tuple [r]
  [(TopicPartition. (.topic r) (.partition r)) (OffsetAndMetadata. (+ 1 (.offset r)))])

(defn calculate-offsets [buffer]
  (reduce #(conj %1 (commit-tuple %2)) {} buffer))

(defn create-consumer [consume-fn topic]
  (let [consumer (doto (KafkaConsumer. (make-config))
                    (.subscribe [topic]))]
    (loop [lc (lazy-consumer consumer)]
      (let [buffer (take min-batch-size lc)]
        (doseq [record buffer]
          (consume-fn record))
        (.commitSync consumer (calculate-offsets buffer))
        (recur (drop min-batch-size lc))))
    consumer))

(defn create-consumer-thread [consume-fn topic]
  (let [thread (Thread. (fn []
                          (create-consumer consume-fn topic)))]
    (.start thread)
    thread))