(ns app.auth.login.verify-sms.inter)

(defmulti send-code!
  "Send a verification code to :verify-sms/phone-number"
  :verify-sms/impl)

(defmulti verify-code!
  "Verify the given :verify-sms/code against :verify-sms/phone-number"
  :verify-sms/impl)
