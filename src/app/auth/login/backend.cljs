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
   [app.user.entity :as user]))

(rpc/reg
 :rpc/send-code
 (fn [req]
   (a/go
     (let [sent (a/<! (verify-sms/send-code! req))]
       sent))))

(rpc/reg
 :rpc/verify-code
 (fn [{:keys [verify-sms/phone-number session/session-id] :as i}]
   (a/go
     (let [verified (a/<! (verify-sms/verify-code! i))
           user-existing (a/<! (user-db/find-by-phone-number! i phone-number))
           user-new (user/create-from-phone-number phone-number)
           user (merge user-new user-existing)
           session-new (session/create {:session/user-id (:user/user-id user)
                                        :session/session-id session-id})
           res (if (result/ok? verified) (merge verified user) verified)]

       (when (result/ok? res)
         (a/<! (user-db/put! i user))
         (a/<! (session-db/put! i session-new)))

       res))))
