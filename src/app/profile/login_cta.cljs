(ns app.profile.login-cta
  (:require
   [core.ui.icon :as icon]
   [core.ui.button :as button]))


(defn view [{:keys [put!]}]
  [:div.w-full.flex-1.flex.flex-col.items-center.justify-center.gap-4.p-6
   [icon/door-open {:class "size-20"}]
   [:p.font-bold.text-xl "Log in to access your profile"]
   [button/view
    {:button/label "Log in"
     :button/on-click #(put! [:screen/clicked-link [:screen/login]])}]])
  