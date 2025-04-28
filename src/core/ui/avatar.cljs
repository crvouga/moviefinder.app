(ns core.ui.avatar
  (:require
   [core.ui.icon :as icon]))

(defn- view-image [i]
  [:img {:alt (:avatar/alt i)
         :src (:avatar/src i)
         :class (str "aspect-square rounded-full overflow-hidden " (:avatar/class i))}])


(defn- view-icon [i]
  [:div {:class (str "aspect-square rounded-full overflow-hidden bg-neutral-600 grid place-items-center " (:avatar/class i))}
   [icon/user-circle {:class "w-full h-full"}]])


(defn- image? [i]
  (not-empty (:avatar/src i)))

(defn view [i]
  (if (image? i)
    (view-image i)
    (view-icon i)))
