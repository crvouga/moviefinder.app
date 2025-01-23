(ns moviefinder-app.auth.login.verify-sms.interface)

(defmulti send-code! :verify-sms/impl)

(defmulti verify-code! :verify-sms/impl)

(defmulti error->message first)

(defmethod error->message :verify-sms-error/wrong-code []
  "Wrong code")