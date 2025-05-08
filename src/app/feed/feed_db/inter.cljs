(ns app.feed.feed-db.inter)

(defmulti new! :feed-db/impl)
(defmulti get! :feed-db/impl)
(defmulti put! :feed-db/impl)