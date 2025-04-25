(ns app.frontend.db
  (:require
   [app.frontend.mod :as mod]
   [core.program :as p]))

(defn- map-vals [f m]
  (into {} (map (fn [[k v]] [k (f v)]) m)))

(defn- to-primary-key [i]
  (-> i :query-result/primary-key))

(defn- query-to-key [query]
  (select-keys query [:query/where
                      :query/order
                      :query/limit
                      :query/offset]))

(def query-result-keys [:query-result/limit
                        :query-result/offset
                        :query-result/total
                        :query-result/primary-key])

(defn- reducer-query-result-by-query [query-result-by-query msg-payload]
  (let [query (-> msg-payload :query-result/query query-to-key)
        primary-key (-> msg-payload :query-result/primary-key)
        entity-ids (->> msg-payload :query-result/rows (map primary-key))
        query-result (-> msg-payload (select-keys query-result-keys) (assoc :query-result/row-ids entity-ids))]
    (assoc query-result-by-query query query-result)))

(defn- to-entity-by-id [msg-payload]
  (->> msg-payload
       :query-result/rows
       (group-by (to-primary-key msg-payload))
       (map-vals first)
       (into {})))

(defn- reducer-entity-by-id [entity-by-id msg-payload]
  (merge-with merge entity-by-id (to-entity-by-id msg-payload)))


(defn- init [state]
  (-> state
      (update ::entity-by-id #(or % {}))
      (update ::query-result-by-query #(or % {}))))

(defn tap-print [input]
  input)

(defn reducer-got-query-result [state [_ msg-payload]]
  (-> state
      (init)
      (update ::entity-by-id reducer-entity-by-id msg-payload)
      (update ::query-result-by-query reducer-query-result-by-query msg-payload)
      tap-print))

(defn- logic [i]
  (p/reg-reducer i ::got-query-result reducer-got-query-result))

(defn to-query-result [state query]
  (let [query-result (-> state ::query-result-by-query (get (query-to-key query)))
        entities (->> query-result :query-result/row-ids (map (-> state ::entity-by-id)))]
    (-> query-result
        (assoc :query-result/rows entities))))

(defn put-query-result! [i query-result]
  (p/put! i [::got-query-result query-result]))

(defn to-entity [state entity-id]
  (-> state ::entity-by-id (get entity-id)))

(mod/reg
 {:mod/name :mod/db
  :mod/logic-fn logic})