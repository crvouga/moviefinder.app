use super::{controls, core::Feed, feed_id::FeedId, feed_item::FeedItem, route::Route};
use crate::{
    core::{
        html::*,
        res::Res,
        ui::{self, image::Image},
    },
    ctx::Ctx,
    media::{self, media_db::interface::MediaQuery, media_id::MediaId},
    req::Req,
    route,
    ui::bottom_bar,
    user_session::session_id::SessionId,
};

pub async fn respond(ctx: &Ctx, req: &Req, route: &Route) -> Res {
    match route {
        Route::DefaultLoad => {
            let res: Res = view_default_load().into();
            res.cache()
        }

        Route::Default => {
            let maybe_feed_id = ctx
                .session_feed_mapping_db
                .get(req.session_id.clone())
                .await
                .unwrap_or(None);

            let feed_id = maybe_feed_id.unwrap_or_default();

            let index_route = route::Route::Feed(Route::IndexLoad {
                feed_id: feed_id.clone(),
            });

            let res = respond_index(ctx, req, &feed_id).await;

            res.hx_push_url(&index_route.encode())
        }

        Route::IndexLoad { feed_id } => {
            let res: Res = view_load(&feed_id).into();
            res.cache()
        }

        Route::Index { feed_id } => respond_index(ctx, req, feed_id).await,

        Route::ChangedSlide { feed_id } => {
            let maybe_slide_index = req
                .form_data
                .get_first("feed_index")
                .and_then(|s| s.parse::<usize>().ok());

            let slide_index_new = match maybe_slide_index {
                None => return Res::empty(),

                Some(slide_index_new) => slide_index_new,
            };

            let feed = ctx.feed_db.get_else_default(feed_id.clone()).await;

            let feed_new = Feed {
                start_index: slide_index_new,
                ..feed
            };

            put_feed(ctx, &req.session_id, &feed_new).await;

            Res::empty()
        }

        Route::LoadMore {
            feed_id,
            start_feed_index,
        } => {
            let feed = ctx.feed_db.get_else_default(feed_id.clone()).await;

            let feed_with_new_index = Feed {
                start_index: *start_feed_index,
                ..feed
            };

            let got = get_feed_items(ctx, &feed_with_new_index).await;

            match got {
                Err(err) => ui::error::page(&err).into(),

                Ok(feed_items) => {
                    let res = view_feed_items(feed_id, &feed_items).into();

                    res
                }
            }
        }

        Route::Controls { feed_id, child } => {
            controls::respond::respond(ctx, req, feed_id, child).await
        }
    }
}

async fn respond_index(ctx: &Ctx, req: &Req, feed_id: &FeedId) -> Res {
    let feed = ctx.feed_db.get_else_default(feed_id.clone()).await;

    put_feed(ctx, &req.session_id, &feed).await;

    let initial_feed_items = get_feed_items(ctx, &feed).await.unwrap_or_default();

    let model = ViewModel {
        feed: feed.clone(),
        initial_feed_items,
    };

    view_feed(&model).into()
}

async fn get_feed_items(ctx: &Ctx, feed: &Feed) -> Result<Vec<FeedItem>, String> {
    let query: MediaQuery = feed.into();

    let queried = ctx.media_db.query(query).await;

    match queried {
        Err(err) => Err(err),

        Ok(paginated) => {
            let feed_items = paginated
                .items
                .into_iter()
                .enumerate()
                .map(|(index, media)| FeedItem::from((media, index + feed.start_index)))
                .collect::<Vec<FeedItem>>();

            Ok(feed_items)
        }
    }
}

async fn put_feed(ctx: &Ctx, session_id: &SessionId, feed: &Feed) {
    let put_feed_fut = ctx.feed_db.put(feed.clone());
    let put_session_mapping_fut = ctx
        .session_feed_mapping_db
        .put(session_id.clone(), feed.feed_id.clone());
    let _ = tokio::join!(put_feed_fut, put_session_mapping_fut);
}

struct ViewModel {
    feed: Feed,
    initial_feed_items: Vec<FeedItem>,
}

const INDEX_ID: &str = "feed";
fn index_selector() -> String {
    format!("#{}", INDEX_ID)
}

fn view_top_bar_root() -> Elem {
    button().class(
        "w-full h-16 shrink-0 border-b flex items-center justify-center relative pl-2 overflow-hidden",
    )
}

fn view_top_bar_link_root(feed_id: &FeedId) -> Elem {
    view_top_bar_root()
        .root_push_screen(route::Route::Feed(Route::Controls {
            feed_id: feed_id.clone(),
            child: controls::route::Route::LoadIndex,
        }))
        .hx_abort(&index_selector())
}

fn view_top_bar(model: &ViewModel) -> Elem {
    view_top_bar_link_root(&model.feed.feed_id)
        .child(view_chips(&model))
        .child(view_open_controls_button())
}

fn view_root() -> Elem {
    div()
        .class("w-full flex-1 flex items-center justify-center flex-col overflow-hidden")
        .id(INDEX_ID)
}

fn view_empty_slide() -> Elem {
    Image::view().src(" ").class("w-full h-full object-cover")
}

fn view_load(feed_id: &FeedId) -> Elem {
    view_root()
        .root_swap_screen(route::Route::Feed(Route::Index {
            feed_id: feed_id.clone(),
        }))
        .hx_trigger_load()
        .child(view_top_bar_link_root(&feed_id).child(view_open_controls_button()))
        .child(view_empty_slide())
        .child(view_bottom_bar())
}

fn view_default_load() -> Elem {
    view_root()
        .root_swap_screen(route::Route::Feed(Route::Default))
        .hx_trigger_load()
        .child(view_top_bar_root().child(view_open_controls_button()))
        .child(view_empty_slide())
        .child(view_bottom_bar())
}

fn view_open_controls_button() -> Elem {
    div()
        .class("absolute top-0 right-0 h-full flex items-center justify-center")
        .child(div().class("w-16 h-full from-transparent to-black bg-gradient-to-r"))
        .child(
            div()
                .class("size-16 bg-black flex items-center justify-center")
                .child(ui::icon::adjustments_vertical("size-8")),
        )
}

fn view_chips(model: &ViewModel) -> Elem {
    div()
        .class("flex flex-row gap-2 p-2 flex-1 overflow-hidden")
        .children(
            model
                .feed
                .filters
                .iter()
                .map(|filter| filter.chip().disabled(true).checked(true).view())
                .collect::<Vec<Elem>>(),
        )
}

fn view_feed(model: &ViewModel) -> Elem {
    view_root()
        .child(view_top_bar(&model))
        .child(view_swiper(&model))
        .child(view_bottom_bar())
}

fn view_bottom_bar() -> Elem {
    bottom_bar::view(bottom_bar::Active::Home, &index_selector())
}

fn view_swiper(model: &ViewModel) -> Elem {
    ui::swiper::container()
        .swiper_direction_vertical()
        .swiper_slides_per_view("1")
        .class("flex-1 flex flex-col w-full items-center justify-center overflow-hidden")
        .hx_trigger_custom("swiperslidechange from:swiper-container")
        .x_on("swiperslidechange", r#"
            feedActiveIndex = parseInt(event?.detail?.[0]?.slides?.[event?.detail?.[0]?.activeIndex]?.getAttribute?.('data-feed-index'), 10);
            // console.log('feedIndex', feedActiveIndex, 'raw', event?.detail?.[0]?.slides?.[event?.detail?.[0]?.activeIndex]?.getAttribute?.('data-feed-index'));
        "#)
        .hx_swap_none()
        .hx_post(
            route::Route::Feed(Route::ChangedSlide {
                feed_id: model.feed.feed_id.clone(),
            })
            .encode()
            .as_str(),
        )
        .hx_vals(
            "js:{feed_index: parseInt(event?.detail?.[0]?.slides?.[event?.detail?.[0]?.activeIndex]?.getAttribute?.('data-feed-index'), 10)}"
        )
        .child(view_feed_items(&model.feed.feed_id, &model.initial_feed_items))
}

fn view_feed_items(feed_id: &FeedId, feed_items: &[FeedItem]) -> Elem {
    frag()
        .children(feed_items.iter().map(view_feed_item).collect::<Vec<Elem>>())
        .child(
            feed_items
                .iter()
                .last()
                .map(|feed_item| view_load_more(feed_id, feed_item.to_feed_index() + 1))
                .unwrap_or(frag()),
        )
}

fn view_load_more(feed_id: &FeedId, start_feed_index: usize) -> Elem {
    ui::swiper::slide()
        .class("flex-1 flex flex-col items-center justify-center")
        .hx_get(
            &route::Route::Feed(Route::LoadMore {
                feed_id: feed_id.clone(),
                start_feed_index,
            })
            .encode(),
        )
        .hx_trigger_intersect()
        .hx_swap_outer_html()
        .child(view_empty_slide())
}

fn to_media_details_route(media_id: &MediaId) -> route::Route {
    route::Route::Media(media::route::Route::Details(
        media::details::route::Route::IndexLoad {
            media_id: media_id.clone(),
        },
    ))
}

fn view_feed_item(feed_item: &FeedItem) -> Elem {
    ui::swiper::slide()
        .class("w-full h-full flex flex-col items-center justify-center cursor-pointer relative")
        .attr("data-feed-index", &feed_item.to_feed_index().to_string())
        .child(view_feed_item_content(feed_item))
}

fn view_feed_item_content(feed_item: &FeedItem) -> Elem {
    match feed_item {
        FeedItem::Media {
            media,
            feed_index: _,
        } => button()
            .class("w-full h-full")
            .root_push_screen(to_media_details_route(&media.media_id))
            .child(
                Image::view()
                    .class("w-full h-full object-cover")
                    .width("100%")
                    .height("100%")
                    .src(media.media_poster.to_highest_res()),
            ),
    }
}
