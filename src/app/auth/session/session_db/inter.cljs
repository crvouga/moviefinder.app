(ns app.auth.session.session-db.inter)

(defmulti init! :session-db/impl)
(defmulti find-by-session-id! :session-db/impl)
(defmulti put! :session-db/impl)
(defmulti zap! :session-db/impl)