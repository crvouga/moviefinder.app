(ns app.user.user-db.inter)

(defmulti find-by-id! :user-db/impl)
(defmulti find-by-phone-number! :user-db/impl)
(defmulti put! :user-db/impl)
(defmulti zap! :user-db/impl)
