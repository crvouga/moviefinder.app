(ns app.feed.feed-db.inter)

(defmulti init :feed-db/impl)
(defmulti get! :feed-db/impl)
(defmulti put! :feed-db/impl)