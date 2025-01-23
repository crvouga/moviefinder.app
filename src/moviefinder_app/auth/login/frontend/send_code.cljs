(ns moviefinder-app.auth.login.frontend.send-code
  (:require
   [moviefinder-app.frontend.screen :as screen]
   [moviefinder-app.frontend.store :as store]
   [moviefinder-app.core.result :as result]
   [moviefinder-app.core.ui.button :as button]
   [moviefinder-app.auth.login.frontend.shared :refer [view-layout]]
   [moviefinder-app.frontend.toast :as toast]
   [moviefinder-app.core.ui.text-field :as text-field]))

(store/reg!
 :store/initialized
 (fn [i]
   (-> i
       (assoc-in [:store/state ::send-code] [:result/not-asked])))

 ::submitted-send-code-form
 (fn [i]
   (-> i
       (update-in [:store/state] assoc ::send-code [:result/loading])
       (update-in [:store/effs] conj [:rpc/send! {:rpc/req [:login-rpc/send-code {:user/phone-number (-> i :store/state ::phone-number)}]
                                                  :rpc/res #(vector ::backend-sent-code %)}])))

 ::backend-sent-code
 (fn [i]
   (let [sent-code (store/msg-payload i)
         msgs (when (result/ok? sent-code)
                [[:screen/push [:route/login-verify-code (result/payload sent-code)]]
                 [:toaster/show (toast/info "Code sent")]])]
     (-> i
         (update-in [:store/state] assoc ::send-code sent-code)
         (update-in [:store/msgs] concat msgs))))


 ::inputted-phone-number
 (fn [i]
   (-> i
       (assoc-in [:store/state ::phone-number] (store/msg-payload i)))))


(defn sending-code? [i]
  (-> i :store/state ::send-code first (= :result/loading)))

(screen/reg!
 :screen/login
 (fn [i]
   [view-layout "Login with SMS"
    [:form.flex.flex-col.w-full.gap-6
     {:on-submit #(do (.preventDefault %) (store/put! i [::submitted-send-code-form]))}
     [text-field/view
      {:text-field/label "Phone Number"
       :text-field/value (-> i :store/state ::phone-number (or ""))
       :text-field/required? true
       :text-field/placeholder "Phone Number"
       :text-field/disabled? (sending-code? i)
       :text-field/on-change #(store/put! i [::inputted-phone-number %])}]
     [:div.w-full]
     [button/view
      {:button/type :button-type/submit
       :button/loading? (sending-code? i)
       :button/label "Send Code"}]]]))
