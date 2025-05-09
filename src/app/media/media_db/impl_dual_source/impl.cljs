(ns app.media.media-db.impl-dual-source.impl
  (:require [app.media.media-db.inter :as media-db]
            [clojure.core.async :refer [go <! >! chan close!]]))

(defmethod media-db/query! :media-db-/impl-dual-source [inst q]
  (let [result-chan (chan)]
    (go
      (let [primary (-> inst :media-db/primary-source)
            secondary (-> inst :media-db/secondary-source)
            primary-result (<! (media-db/query! primary q))]
        (>! result-chan primary-result)
        #_(println "primary-result" primary-result)
        (go
          (let [secondary-result (<! (media-db/query! secondary q))]
            (doseq [entity (:query-result/rows secondary-result)]
              #_(println "putting entity into primary" (-> entity :media/title))
              (<! (media-db/put! (assoc primary :media/entity entity))))))

        (close! result-chan)))
    result-chan))