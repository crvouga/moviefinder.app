(ns app.user.user-db.inter)

(defmulti new! :user-db/impl)
(defmulti find-by-user-id! :user-db/impl)
(defmulti find-by-phone-number! :user-db/impl)
(defmulti put! :user-db/impl)
(defmulti zap! :user-db/impl)
