(ns app.user.user-db.impl-kv
  (:require
   [app.user.user-db.inter :as user-db]
   [lib.kv.inter :as kv]
   [clojure.core.async :refer [<! go]]))


(defn- new-by-user-id-kv [kv]
  (->> {:kv/namespace ::by-user-id}
       (merge kv)
       kv/init))

(defn- new-index-user-id-by-phone-number [kv]
  (->> {:kv/namespace ::by-user-ids-by-phone-number}
       (merge kv)
       kv/init))


(defmethod user-db/init! :user-db/impl-kv
  [config]
  (-> config
      (assoc :user-db/by-user-id-kv (new-by-user-id-kv config))
      (assoc :user-db/by-phone-number-kv (new-index-user-id-by-phone-number config))))

(defmethod user-db/find-by-user-id! :user-db/impl-kv
  [{:keys [user-db/by-user-id-kv]} user-id]
  (kv/get! by-user-id-kv user-id))

(defmethod user-db/find-by-phone-number! :user-db/impl-kv
  [{:keys [user-db/by-phone-number-kv] :as inst} phone-number]
  (go
    (when-let [{:keys [user/user-id]} (<! (kv/get! by-phone-number-kv phone-number))]
      (<! (user-db/find-by-user-id! inst user-id)))))

(defmethod user-db/put! :user-db/impl-kv
  [{:keys [user-db/by-user-id-kv user-db/by-phone-number-kv]}
   {:keys [:user/user-id :user/phone-number] :as user}]
  (go
    (<! (kv/set! by-user-id-kv user-id user))
    (<! (kv/set! by-phone-number-kv phone-number user))))

(defmethod user-db/zap! :user-db/impl-kv
  [{:keys [user-db/by-user-id-kv user-db/by-phone-number-kv]} user-id]
  (go
    (when-let [user (<! (kv/get! by-user-id-kv user-id))]
      (<! (kv/zap! by-phone-number-kv (:user/phone-number user))))
    (<! (kv/zap! by-user-id-kv user-id))))
