(ns app.profile.login-cta
  (:require
   [lib.ui.icon :as icon]
   [lib.ui.button :as button]
   [lib.program :as p]))


(defn view [i]
  [:div.w-full.flex-1.flex.flex-col.items-center.justify-center.gap-4.p-6
   [icon/door-open {:class "size-20"}]
   [:p.font-bold.text-xl "Log in to access your profile"]
   [button/view
    {:button/label "Log in"
     :button/on-click #(p/put! i [:screen/clicked-link [:screen/login]])}]])
  