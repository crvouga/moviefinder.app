(ns app.media.media-db.impl-rpc.backend
  (:require [clojure.core.async :refer [<! go]]
            [app.media.media-db.impl-tmdb-api.impl]
            [app.media.media-db.inter :as media-db]
            [app.rpc.backend :as rpc]))

(rpc/reg-fn
 :rpc-fn/media-db-query
 (fn [ctx q]
   (go
     (let [res (<! (media-db/query! ctx q))]
       res))))
