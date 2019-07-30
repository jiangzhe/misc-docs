use actix_files::NamedFile;
use actix_web::{HttpRequest, Result};
use std::path::PathBuf;
use actix_files as fs;

fn main() {
    run_server();
}

fn run_server() {
    use actix_web::{web, App, HttpServer};

    HttpServer::new(|| App::new()
            .service(fs::Files::new("/static", ".").show_files_listing()))
        .bind("127.0.0.1:8088")
        .unwrap()
        .run()
        .unwrap()
}


fn index(req: HttpRequest) -> Result<NamedFile> {
    let path: PathBuf = req.match_info().query("filename").parse().unwrap();
    Ok(NamedFile::open(path)?)
}
