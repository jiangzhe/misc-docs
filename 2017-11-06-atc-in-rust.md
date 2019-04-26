---
layout: post
title: "Rust中的ATC"
date: 2017-11-06 17:06:00 +0800
categories: Rust
permalink: /:categories/:title
---

std::iter下有两个特质IntoIterator和FromIterator。
以std::collections::LinkedList为例，我们看看Rust是如何设计和实现这种互操作的：

```rust
pub trait IntoIterator {
  type Item;
  type IntoIter: Iterator<Item=Self::Item>;
  fn into_iter(self) -> Self::IntoIter;
}
impl<T> IntoIterator for LinkedList<T> {
  type Item = T;
  type IntoIter = IntoIter<T>;
  ...
}
impl<'a, T> IntoIterator for &'a LinkedList<T> {
  type Item = &'a T;
  type IntoIter = Iter<'a, T>;
  ...
}
impl<'a, T> IntoIterator for &'a mut LinkedList<T> {
  type Item = &'a mut T;
  type IntoIter = IterMut<'a, T>;
  ...
}
```

首先分析一下IntoIterator，它包含了两个associate type：Item和IntoIter。

TBC
