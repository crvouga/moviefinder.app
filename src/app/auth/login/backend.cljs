(ns app.auth.login.backend
  (:require
   [app.auth.login.verify-sms.impl]
   [app.auth.login.verify-sms.inter :as verify-sms]
   [app.auth.session.session-db.inter :as session-db]
   [app.rpc.backend :as rpc]
   [app.user.user-db.inter :as user-db]
   [app.auth.session.entity :as session]
   [clojure.core.async :as a]
   [lib.result :as result]
   [app.user.user :as user]))

(rpc/reg-fn
 :rpc-fn/send-code
 (fn [ctx]
   (a/go
     (let [sent (a/<! (verify-sms/send-code! ctx))]
       sent))))

(rpc/reg-fn
 :rpc-fn/verify-code
 (fn [{:keys [session/session-id] :as ctx}
      {:keys [verify-sms/phone-number] :as payload}]
   (a/go
     (let [verified (a/<! (verify-sms/verify-code! ctx payload))
           user-existing (a/<! (user-db/find-by-phone-number! ctx phone-number))
           user-new (user/create-from-phone-number phone-number)
           user (merge user-new user-existing)
           session-new (session/create {:session/user-id (:user/user-id user)
                                        :session/session-id session-id})
           res (if (result/ok? verified) (merge verified user) verified)]

       (when (result/ok? res)
         (a/<! (user-db/put! ctx user))
         (a/<! (session-db/put! ctx session-new)))

       res))))
