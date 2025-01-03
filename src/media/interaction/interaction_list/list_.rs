use serde::{Deserialize, Serialize};

use crate::{
    core::html::{div, Html},
    list::{list::List, list_id::ListId, list_item_id::ListItemId, list_screen},
    media::{interaction::interaction_name::InteractionName, media_id::MediaId},
    ui::route::AppRoute,
    user::{account_screen, user_id::UserId},
};

use super::route::Route;

#[derive(Debug, Clone, PartialEq, Eq, Serialize, Deserialize)]
pub struct MediaInteractionList {
    pub interaction_name: InteractionName,
    pub user_id: UserId,
}

impl List for MediaInteractionList {
    fn view_art(&self, class: &str) -> Html {
        div()
            .class("bg-gradient-to-br from-[#D38ABF] via-[#434EA9] to-[#07413A]")
            .class(class)
            .child(
                div()
                    .class("w-full h-full p-4 flex flex-col items-center justify-center")
                    .child(self.interaction_name.view_icon(true, "w-full")),
            )
    }

    fn id(&self) -> ListId {
        let name_str = self.interaction_name.to_machine_string();
        let list_id_str = format!("interaction-list-{}-{}", name_str, self.user_id.as_str());
        let list_id = ListId::new(&list_id_str);
        list_id
    }

    fn name(&self) -> String {
        self.interaction_name.to_display_string()
    }

    fn details_url(&self) -> String {
        Route::ListScreen(list_screen::route::Route::Screen {
            back_url: account_screen::route::Route::Screen.url(),
            list: MediaInteractionList {
                user_id: self.user_id.clone(),
                interaction_name: self.interaction_name.clone(),
            },
        })
        .url()
    }
}

impl InteractionName {
    pub fn to_list_item_id(&self, list_id: ListId, media_id: MediaId) -> ListItemId {
        let name_str = self.to_machine_string();

        let list_item_id_str = format!(
            "interaction-list-item-{}-{}-{}",
            name_str,
            list_id.as_str(),
            media_id.as_str()
        );

        let list_item_id = ListItemId::from_string(&list_item_id_str);

        list_item_id
    }
}
