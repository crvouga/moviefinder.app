use crate::{
    core::{html::*, ui::icon},
    route::Route,
};

#[derive(Default)]
pub struct TopBar {
    back_route: Option<Route>,
    title: Option<String>,
    cancel_route: Option<Route>,
}

impl TopBar {
    pub fn back_button(mut self, back_route: Route) -> Self {
        self.back_route = Some(back_route);
        self
    }

    pub fn title(mut self, title: &str) -> Self {
        self.title = Some(title.to_string());
        self
    }

    pub fn view(self) -> Elem {
        let back_button_elem = self.back_route.map_or(Empty::view(), BackButton::view);

        let title_elem = self
            .title
            .map_or(div().class("flex-1 truncate"), |s| Title::view(&s));

        let cancel_button_elem = self
            .cancel_route
            .map_or(Empty::view(), |route| CancelButton::new(route).view());

        div()
        .class("flex items-center justify-center w-full border-b h-16 font-bold text-lg text-center truncate")
        .child(back_button_elem)
        .child(title_elem)
        .child(cancel_button_elem)
    }
}

pub struct BackButton {}

impl BackButton {
    pub fn view(back_route: Route) -> Elem {
        button()
            .class("size-16 flex items-center justify-center")
            .aria_label("go back")
            .root_push_screen(back_route)
            .child(icon::back_arrow("size-8"))
    }
}

#[derive(Default)]
pub struct CancelButton {
    loading_disabled_path: Option<String>,
    cancel_route: Option<Route>,
}

impl CancelButton {
    pub fn new(cancel_route: Route) -> Self {
        Self {
            cancel_route: Some(cancel_route),
            ..Self::default()
        }
    }

    pub fn loading_disabled_path(mut self, loading_disabled_path: &str) -> Self {
        self.loading_disabled_path = Some(loading_disabled_path.to_string());
        self
    }

    pub fn view(self) -> Elem {
        button()
            .class("size-16 flex items-center justify-center")
            .class("disabled:opacity-80 disabled:cursor-not-allowed")
            .map(|elem| match self.cancel_route {
                Some(route) => elem.root_push_screen(route),
                None => elem,
            })
            .aria_label("cancel")
            .child(icon::x_mark("size-8"))
            .map(|elem| {
                if let Some(loading_disabled_path) = self.loading_disabled_path {
                    elem.hx_loading_disabled()
                        .hx_loading_path(&loading_disabled_path)
                } else {
                    elem
                }
            })
    }
}

struct Title {}

impl Title {
    fn view(title: &str) -> Elem {
        div()
            .class("flex-1 text-center flex items-center justify-center h-full truncate max-w-full")
            .child(div().class("w-full truncate").child_text(title))
    }
}

struct Empty {}

impl Empty {
    fn view() -> Elem {
        div().class("size-16")
    }
}
