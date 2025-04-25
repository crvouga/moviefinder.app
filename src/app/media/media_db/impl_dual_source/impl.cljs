(ns app.media.media-db.impl-dual-source.impl
  (:require [app.media.media-db.interface :as media-db]
            [clojure.core.async :refer [go <! >! chan close!]]))

(defmethod media-db/query-result-chan! :media-db-impl/dual-source [q]
  (let [result-chan (chan)]
    (go
      (let [primary (-> q :media-db-impl-dual-source/primary)
            secondary (-> q :media-db-impl-dual-source/secondary)
            primary-result (<! (media-db/query-result-chan! primary))]
        (>! result-chan primary-result)
        (println "primary-result" primary-result)
        (go
          (let [secondary-result (<! (media-db/query-result-chan! secondary))]
            (doseq [entity (:query-result/rows secondary-result)]
              (println "putting entity into primary" (-> entity :media/title))
              (<! (media-db/upsert-chan! (assoc primary :media/entity entity))))))

        (close! result-chan)))
    result-chan))