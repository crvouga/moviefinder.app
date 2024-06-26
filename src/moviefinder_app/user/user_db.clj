(ns moviefinder-app.user.user-db)


(defprotocol UserDb
  (find-by-phone-number! [this phone-number])
  (find-by-email! [this email])
  (find-by-id! [this id])
  (put! [this user]))

(defmulti ->UserDb :user-db/impl)