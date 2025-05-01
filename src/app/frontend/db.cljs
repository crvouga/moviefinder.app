(ns app.frontend.db
  (:require
   [app.frontend.mod :as mod]
   [lib.program :as p]))

(defn- map-vals [f m]
  (into {} (map (fn [[k v]] [k (f v)]) m)))

(defn- to-primary-key [i]
  (-> i :queried/primary-key))

(defn- query-to-key [query]
  (select-keys query [:query/where
                      :query/order
                      :query/limit
                      :query/offset]))

(def query-result-keys [:queried/limit
                        :queried/offset
                        :queried/total
                        :queried/primary-key])

(defn- reducer-query-result-by-query [query-result-by-query msg-payload]
  (let [query (-> msg-payload :queried/query query-to-key)
        primary-key (-> msg-payload :queried/primary-key)
        entity-ids (->> msg-payload :queried/rows (map primary-key))
        query-result (-> msg-payload (select-keys query-result-keys) (assoc :queried/row-ids entity-ids))]
    (assoc query-result-by-query query query-result)))

(defn- to-entity-by-id [msg-payload]
  (->> msg-payload
       :queried/rows
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
  (p/reg-reducer i :db/got-query-result reducer-got-query-result))

(defn to-query-result [state query]
  (let [query-result (-> state ::query-result-by-query (get (query-to-key query)))
        entities (->> query-result :queried/row-ids (map (-> state ::entity-by-id)))]
    (-> query-result
        (assoc :queried/rows entities))))


(defn to-entity [state entity-id]
  (-> state ::entity-by-id (get entity-id)))

(mod/reg
 {:mod/name :mod/db
  :mod/logic-fn logic})