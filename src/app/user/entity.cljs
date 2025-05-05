(ns app.user.entity
  (:require
   [cljs.spec.alpha :as s]
   [lib.time :as time]))

(s/def :user/user-id string?)
(s/def :user/phone-number string?)
(s/def :user/created-at string?)
(s/def :user/updated-at string?)


(s/def :user/entity
  (s/keys :opt [:user/user-id
                :user/phone-number
                :user/created-at
                :user/updated-at]))




(defn create [{:keys [user/phone-number]}]
  (let [username (str "movie finder " (rand-int 1000000))]
    {:user/phone-number phone-number
     :user/user-id (str (random-uuid))
     :user/username username
     :user/avatar-seed username
     :user/created-at (time/now!)}))

(defn create-from-phone-number [phone-number]
  (create {:user/phone-number phone-number}))

(defn eq? [a b]
  (= (select-keys a [:user/phone-number :user/user-id])
     (select-keys b [:user/phone-number :user/user-id])))

(defn edit [user edits]
  (merge user (select-keys edits [:user/avatar-seed :user/fullname :user/username])))
