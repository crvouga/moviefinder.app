(ns app.auth.user-session-db.interface)

(defmulti new! :user-session-db/impl)
(defmulti find-by-session-id :user-session-db/impl)
(defmulti put! :user-session-db/impl)
(defmulti zap! :user-session-db/impl)

