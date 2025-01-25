(ns moviefinder-app.media.media-db.interface)



(defmulti query :media-db/impl)