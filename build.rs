use std::fs::{self, File};
use std::env;
use std::path::Path;
use std::io;
use std::io::prelude::*;
use std::convert::AsRef;
use comrak::{markdown_to_html, ComrakOptions};

fn main() -> io::Result<()> {
    // let curr_dir = env::current_dir()?;
    // println!("Build started in directory: {:?}", curr_dir);
    if let Ok(genHtml) = env::var("GEN_HTML") {
        if genHtml == "true" {
            gen_html("docs", "static")?;
        }
    }
    Ok(())
}

fn gen_html<P1, P2>(src: P1, tgt: P2) -> io::Result<()> 
where P1: AsRef<Path>, P2: AsRef<Path> {
    fs::create_dir_all(tgt.as_ref())?;
    for entry in fs::read_dir(src)? {
        let entry = entry?;
        let metadata = entry.metadata()?;
        let tgt_path = format!("{}/{}", tgt.as_ref().to_str().unwrap(), entry.file_name().to_str().unwrap());
        if metadata.is_dir() {
            println!("Copying directory {:?} to {:?}", entry.path(), tgt_path);
            gen_html(entry.path(), tgt_path)?;
        } else if metadata.is_file() {
            println!("Copying file {:?} to {:?}", entry.path(), tgt_path);
            if let Some(ext) = entry.path().extension() {
                if let Some(ext_str) = ext.to_str() {
                    if ext_str == "md" {
                        println!("converting markdown file {:?} to html", entry.path());
                        let new_file_name = format!("{}.html", entry.path().file_stem().unwrap().to_str().unwrap());
                        convert_html(entry.path(), format!("{}/{}", tgt.as_ref().to_str().unwrap(), new_file_name))?;
                    } else {
                        println!("copy raw file {:?} to {:?}", entry.path(), tgt_path);
                        fs::copy(entry.path(), tgt_path)?;
                    }
                }
            }
        }
    }
    Ok(())
}

fn convert_html<P1, P2>(src: P1, tgt: P2) -> io::Result<()>
where P1: AsRef<Path>, P2: AsRef<Path> {
    Ok(())
}