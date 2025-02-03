(ns app.auth.login.verify-sms.interface)

(defmulti send-code!
  "Send a verification code to :user/phone-number"
  :verify-sms/impl)

(defmulti verify-code!
  "Verify the given :verify-sms/code against :user/phone-number"
  :verify-sms/impl)
