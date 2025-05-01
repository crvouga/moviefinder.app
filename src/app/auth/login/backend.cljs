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
     (let [res (a/<! (verify-sms/send-code! req))]
       res))))

(rpc/reg
 :rpc/verify-code
 (fn [{:keys [user/phone-number session/session-id] :as i}]
   (a/go
     (let [verified (a/<! (verify-sms/verify-code! i))
           existing-user (a/<! (user-db/find-by-phone-number! i phone-number))
           new-user (user/create-from-phone-number phone-number)
           user (merge new-user existing-user)
           {:keys [user/user-id]} user
           session-new (session/create {:session/user-id user-id
                                        :session/session-id session-id})
           res (if (result/ok? verified) (merge verified user) verified)]

       (when (result/ok? res)
         #_(a/<! (user-db/put! i user))
         (a/<! (session-db/put! i session-new)))

       res))))
