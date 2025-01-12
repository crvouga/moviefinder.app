(defmulti send-code! :auth-service/impl)

(defmulti verify-code! :auth-service/impl)

(defmethod send-code! :default [_i]
  (throw (ex-info "send-code! not implemented" {})))

(defmethod verify-code! :default [_i]
  (throw (ex-info "verify-code! not implemented" {})))

(defmethod send-code! :auth-service-impl/rpc [i]
  i)