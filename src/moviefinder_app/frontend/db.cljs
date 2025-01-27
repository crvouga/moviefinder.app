(ns moviefinder-app.frontend.db
  (:require
   [moviefinder-app.frontend.store :as store]
   [clojure.core.async :refer [go-loop <! >!]]))

(defn- map-vals [f m]
  (into {} (map (fn [[k v]] [k (f v)]) m)))

(defn- to-primary-key [i]
  (-> i  :query-result/primary-key))

(defn- query-to-key [query]
  (select-keys query [:query/where :query/order :query/limit :query/offset]))

(defn- update-entity-ids-by-query [i]
  (let [payload (-> i store/msg-payload)
        query (-> payload :query-result/query query-to-key)
        primary-key (-> payload :query-result/primary-key)
        entity-ids (->> payload :query-result/rows (map primary-key))
        query-result (-> payload
                         (select-keys [:query-result/limit :query-result/offset :query-result/total :query-result/primary-key])
                         (assoc :query-result/row-ids entity-ids))]
    (-> i
        (assoc-in [:store/state ::query-result-by-query query] query-result))))

(defn- to-entity-by-id [i]
  (->> i
       :query-result/rows
       (group-by (to-primary-key i))
       (map-vals first)
       (into {})))


(defn- update-entities-by-id [i]
  (let [payload (-> i store/msg-payload)
        entity-by-id-payload (to-entity-by-id payload)
        entity-by-id (-> i :store/state ::row-by-id)
        entity-by-id-merged (merge-with merge entity-by-id entity-by-id-payload)]
    (-> i
        (assoc-in [:store/state ::row-by-id] entity-by-id-merged))))


(store/register!
 :store/initialized
 (fn [i]
   (-> i
       (update :store/state assoc
               ::row-by-id {}
               ::query-result-by-query {})))


 :db/got-query-result
 (fn [i]
   (-> i
       update-entities-by-id
       update-entity-ids-by-query)))

(defn to-query-result [i query]
  (let [state (-> i :store/state)
        query-result (-> state ::query-result-by-query (get (query-to-key query)))
        entities (->> query-result :query-result/row-ids (map (-> state ::row-by-id)))]
    (-> query-result
        (assoc :query-result/rows entities))))

(defn put-query-result! [query-result-chan!]
  (go-loop []
    (when-let [query-result (<! query-result-chan!)]
      (>! store/msg-chan! [:db/got-query-result query-result])
      (recur))))