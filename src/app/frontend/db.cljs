(ns app.frontend.db
  (:require
   [clojure.core.async :refer [go-loop <!]]
   [core.program :as p]))

(defn- map-vals [f m]
  (into {} (map (fn [[k v]] [k (f v)]) m)))

(defn- to-primary-key [i]
  (-> i :query-result/primary-key))

(defn- query-to-key [query]
  (select-keys query [:query/where :query/order :query/limit :query/offset]))

(defn- update-entity-ids-by-query [state payload]
  (let [query (-> payload :query-result/query query-to-key)
        primary-key (-> payload :query-result/primary-key)
        entity-ids (->> payload :query-result/rows (map primary-key))
        query-result (-> payload
                         (select-keys [:query-result/limit
                                       :query-result/offset
                                       :query-result/total
                                       :query-result/primary-key])
                         (assoc :query-result/row-ids entity-ids))]
    (assoc-in state [::query-result-by-query query] query-result)))

(defn- to-entity-by-id [payload]
  (->> payload
       :query-result/rows
       (group-by (to-primary-key payload))
       (map-vals first)
       (into {})))

(defn- update-entities-by-id [state payload]
  (let [entity-by-id-payload (to-entity-by-id payload)
        entity-by-id (-> state ::entity-by-id)
        entity-by-id-merged (merge-with merge entity-by-id entity-by-id-payload)]
    (assoc state ::entity-by-id entity-by-id-merged)))

(go-loop []
  (let [msg (<! (p/take! :db/got-query-result))
        payload (second msg)
        state (p/read!)
        updated-state (-> state
                          (update-entities-by-id payload)
                          (update-entity-ids-by-query payload))]
    (p/put! [::set-db updated-state])
    (recur)))

(p/reg-reducer ::set-db (fn [state msg] (merge state (second msg))))

#_(p/reg-init
   (fn [state]
     (assoc state
            ::entity-by-id {}
            ::query-result-by-query {})))

(defn to-query-result [state query]
  (let [query-result (-> state ::query-result-by-query (get (query-to-key query)))
        entities (->> query-result :query-result/row-ids (map (-> state ::entity-by-id)))]
    (-> query-result
        (assoc :query-result/rows entities))))

(defn put-query-result! [query-result-chan!]
  (go-loop []
    (when-let [query-result (<! query-result-chan!)]
      (p/put! [::got-query-result query-result])
      (recur))))

(defn to-entity [state entity-id]
  (-> state ::entity-by-id (get entity-id)))