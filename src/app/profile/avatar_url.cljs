(ns app.profile.avatar-url)

(def base-url "https://api.dicebear.com/9.x/fun-emoji/svg?seed=")

(defn to [avatar-seed]
  (str base-url (js/encodeURIComponent avatar-seed)))

(defn from-user [{:keys [user/avatar-seed]}]
  (to avatar-seed))