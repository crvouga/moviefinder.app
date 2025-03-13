(ns app.auth.login.frontend.verify-code
  (:require
   [app.frontend.screen :as screen]
   [app.frontend.store :as store]
   [core.ui.button :as button]
   [core.ui.text-field :as text-field]
   [core.ui.top-bar :as top-bar]
   [app.frontend.toast :as toast]))

(store/register!
 :store/initialized
 (fn [i]
   (-> i
       (assoc-in [:store/state ::request] {:result/type :result/not-asked})))


 ::inputted-code
 (fn [i]
   (-> i
       (assoc-in [:store/state ::code] (store/to-msg-payload i))))


 ::submitted-verify-code-form
 (fn [i]
   (let [rpc-req [:rpc/verify-code
                  {:user/phone-number (-> i screen/screen-payload :user/phone-number)
                   :verify-sms/code (-> i :store/state ::code)}]]
     (-> i
         (update-in [:store/state] assoc ::request {:result/type :result/loading})
         (update-in [:store/effs] conj [:rpc/send! {:rpc/req rpc-req
                                                    :rpc/res #(vector ::backend-verified-code %)}]))))

 ::backend-verified-code
 (fn [i]
   (let [payload (store/to-msg-payload i)
         current-user-id (-> payload :user/id)
         msgs (if (and current-user-id (-> payload :result/type (= :result/ok)))
                ;; 
                [[:login/authenticated payload]
                 [:screen/push [:screen/profile]]
                 [:toaster/show (toast/info "Logged in")]]
                ;; 
                [[:toaster/show (toast/error "Failed to log in")]])]
     (-> i
         (update-in [:store/state] assoc ::request payload)
         (update-in [:store/msgs] concat msgs)))))


(defn loading? [i]
  (-> i :store/state ::request :result/type (= :result/loading)))

(screen/register!
 :screen/login-verify-code
 (fn [i]
   [:div.w-full.flex-1
    [top-bar/view {:top-bar/title "Verify Code"
                   :top-bar/on-back #(store/put! i [:screen/clicked-link [:screen/login]])}]
    [:form.flex.flex-col.w-full.gap-6.p-6
     {:on-submit #(do (.preventDefault %) (store/put! i [::submitted-verify-code-form]))}
     [:p.text-lg "Enter the code we sent to " [:span.font-bold (-> i screen/screen-payload :user/phone-number)]]
     [text-field/view
      {:text-field/label "Code"
       :text-field/value (-> i :store/state ::code)
       :text-field/type :text-field-type/number-pad
       :text-field/required? true
       :text-field/disabled? (loading? i)
       :text-field/on-change #(store/put! i [::inputted-code %])}]
     [:div.w-full]
     [button/view
      {:button/type :button-type/submit
       :button/loading? (loading? i)
       :button/full? true
       :button/label "Verify Code"}]]]))