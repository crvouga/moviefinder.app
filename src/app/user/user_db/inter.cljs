(ns app.user.user-db.inter)

(defmulti new! (fn [inst] (get inst :user-db/impl)))
(defmulti find-by-user-id! (fn [inst _user-id] (get inst :user-db/impl)))
(defmulti find-by-phone-number! (fn [inst _phone-number] (get inst :user-db/impl)))
(defmulti put! (fn [inst _user] (get inst :user-db/impl)))
(defmulti zap! (fn [inst _user-id] (get inst :user-db/impl)))
