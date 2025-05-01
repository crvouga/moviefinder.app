(ns app.user.user-db.impl-kv
  (:require
   [app.user.user-db.inter :as user-db]
   [lib.kv.inter :as kv]
   [clojure.core.async :as a]))


(defn- new-by-user-id-kv [kv]
  (->> {:kv/namespace ::by-user-id}
       (merge kv)
       kv/new!))

(defn- new-index-user-id-by-phone-number [kv]
  (->> {:kv/namespace ::by-user-ids-by-phone-number}
       (merge kv)
       kv/new!))


(defmethod user-db/new! :user-db/impl-kv
  [config]
  (-> config
      (assoc :user-db/by-user-id-kv (new-by-user-id-kv config))
      (assoc :user-db/by-phone-number-kv (new-index-user-id-by-phone-number config))))

(defmethod user-db/find-by-user-id! :user-db/impl-kv
  [{:keys [user-db/by-user-id-kv]} user-id]
  (kv/get! by-user-id-kv user-id))

(defmethod user-db/find-by-phone-number! :user-db/impl-kv
  [{:keys [user-db/by-phone-number-kv] :as inst} phone-number]
  (a/go
    (when-let [{:keys [user/user-id]} (a/<! (kv/get! by-phone-number-kv phone-number))]
      (a/<! (user-db/find-by-user-id! inst user-id)))))

(defmethod user-db/put! :user-db/impl-kv
  [{:keys [user-db/by-user-id-kv user-db/by-phone-number-kv]}
   {:keys [:user/user-id :user/phone-number] :as user}]
  (a/go
    (a/<! (kv/set! by-user-id-kv user-id user))
    (a/<! (kv/set! by-phone-number-kv phone-number user))))

(defmethod user-db/zap! :user-db/impl-kv
  [{:keys [user-db/by-user-id-kv user-db/by-phone-number-kv]} user-id]
  (a/go
    (when-let [user (a/<! (kv/get! by-user-id-kv user-id))]
      (a/<! (kv/zap! by-phone-number-kv (:user/phone-number user))))
    (a/<! (kv/zap! by-user-id-kv user-id))))
