(ns moviefinder-app.media.media-db.interface)

(defmulti query-chan! :media-db/impl)