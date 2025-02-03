(ns app.auth.login.frontend.send-code
  (:require
   [app.frontend.screen :as screen]
   [app.frontend.store :as store]
   [core.ui.button :as button]
   [app.frontend.toast :as toast]
   [core.ui.text-field :as text-field]
   [core.ui.top-bar :as top-bar]))

(store/register!
 :store/initialized
 (fn [i]
   (-> i
       (assoc-in [:store/state ::send-code] {:result/type :result/not-asked})))

 ::submitted-send-code-form
 (fn [i]
   (-> i
       (update-in [:store/state] assoc ::send-code {:result/type :result/loading})
       (update-in [:store/effs] conj [:rpc/send! {:rpc/req [:rpc/send-code {:user/phone-number (-> i :store/state ::phone-number)}]
                                                  :rpc/res #(vector ::backend-sent-code %)}])))

 ::backend-sent-code
 (fn [i]
   (let [sent-code (-> i store/msg-payload)
         msgs (cond
                (-> sent-code :result/type (= :result/ok))
                [[:screen/push [:screen/login-verify-code sent-code]]
                 [:toaster/show (toast/info "Code sent")]]

                (-> sent-code :result/type (= :result/err))
                [[:toaster/show (toast/error (-> sent-code :error/message (or "Failed to send code")))]]

                :else [])]
     (-> i
         (update-in [:store/state] assoc ::send-code sent-code)
         (update-in [:store/msgs] concat msgs))))


 ::inputted-phone-number
 (fn [i]
   (-> i
       (assoc-in [:store/state ::phone-number] (store/msg-payload i)))))


(defn sending-code? [i]
  (-> i :store/state ::send-code :result/type (= :result/loading)))

(screen/register!
 :screen/login
 (fn [i]
   [:div.w-full.flex-1
    [top-bar/view {:top-bar/title "Login with SMS"
                   :top-bar/on-back #(store/put! i [:screen/clicked-link [:screen/profile]])}]
    [:form.flex.flex-col.w-full.gap-6.p-6
     {:on-submit #(do (.preventDefault %) (store/put! i [::submitted-send-code-form]))}
     [text-field/view
      {:text-field/label "Phone Number"
       :text-field/value (-> i :store/state ::phone-number)
       :text-field/required? true
       :text-field/type :text-field-type/number-pad
       :text-field/disabled? (sending-code? i)
       :text-field/on-change #(store/put! i [::inputted-phone-number %])}]
     [:div.w-full]
     [button/view
      {:button/type :button-type/submit
       :button/full? true
       :button/loading? (sending-code? i)
       :button/label "Send Code"}]]]))
