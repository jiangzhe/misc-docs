# 使用Rust对链表进行排序

[Learn Rust With Entirely Too Many Linked Lists]描述了如何在Rust中使用各种方式构建链表。
之前我确实发现用Rust构造一些数据结构有不小的难度。lifetime和borrow check对链表，树，图这样的数据结构不太友好。
所以这几天也用OJ进行了一些实战，有一些收获，记录在这里。

[Sort List]，这是leetcode上的一道题。
这道题要求使用O(nlogn)时间复杂度和常数的空间复杂度来排序一个链表。

给出的数据结构和函数定义如下：
```rust
// Definition for singly-linked list.
#[derive(PartialEq, Eq, Clone, Debug)]
pub struct ListNode {
    pub val: i32,
    pub next: Option<Box<ListNode>>,
}

impl ListNode {
    #[inline]
    fn new(val: i32) -> Self {
        ListNode { next: None, val }
    }
}

pub struct Solution;

impl Solution {
    pub fn sort_list(head: Option<Box<ListNode>>) -> Option<Box<ListNode>> {
        unimplemented!()
    }
}
```

链表的定义和[Learn Rust With Entirely Too Many Linked Lists]里Node的定义是一致的。

考虑如何实现：首先想到的是采用合并排序的思想。
1. 将链表一分为二。
2. 对左右两个链表排序。(递归调用)
3. 合并两个链表。

简单计算一下，时间复杂度满足要求，但是递归调用占用了logn的程序栈空间，严格地说空间复杂度是不满足的。

因此考虑用迭代方式替换递归。
递归是自上而下的思路，而迭代则由下而上。

迭代的思路：
1. 对链表以步长2划分，并合并排序使每个步长作为一个子链表是有序的。
2. 对链表以前一次步长的2倍划分，合并已排序的左右两个长度等于前一次步长的链表，保证其有序。
3. 重复第2步，直到前一次的步长已超过链表总长。此时整个链表即有序的。

但使用Rust还有一个问题，需要考虑链表指针的操作符合ownership和borrow check的约束。
在和编译器的不断斗争中最终实现如下，没有使用unsafe。
1. 通过```&mut T```遍历链表。
2. ```Option::take(&mut self)```，在方法中获取链表所有权。
3. ```Option::replace(&mut self, T)```，将链表转移回原可变引用。

```rust
// Definition for singly-linked list.
#[derive(PartialEq, Eq, Clone, Debug)]
pub struct ListNode {
    pub val: i32,
    pub next: Option<Box<ListNode>>,
}

impl ListNode {
    #[inline]
    fn new(val: i32) -> Self {
        ListNode { next: None, val }
    }
}

pub struct Solution;

impl Solution {
    pub fn sort_list(head: Option<Box<ListNode>>) -> Option<Box<ListNode>> {
        if head.is_none() {
            return None;
        }
        let len = list_length(&head);
        let mut head = head;
        let mut step = 1;
        while step < len {
            let mut curr = &mut head;
            while curr.is_some() {
                let left = split(curr, step);
                let right = split(curr, step);
                curr = merge(curr, left, right);
            }
            step <<= 1;
        }
        head
    }
}

fn list_length(head: &Option<Box<ListNode>>) -> usize {
    let mut len = 0;
    let mut curr = head;
    while let Some(ref box_node) = curr {
        len += 1;
        curr = &box_node.next;
    }
    len
}

fn split(head: &mut Option<Box<ListNode>>, step: usize) -> Option<Box<ListNode>> {
    if head.is_none() {
        return None;
    }
    let mut list = head.take();

    let mut new_list = &mut list;
    let mut i = 0;
    while let Some(ref mut box_node) = new_list {
        i += 1;
        if i == step {
            if let Some(rest) = box_node.next.take() {
                head.replace(rest);
            }
            return list;
        } else {
            new_list = &mut box_node.next;
        }
    }
    list
}

fn merge<'a>(
    head: &'a mut Option<Box<ListNode>>,
    left: Option<Box<ListNode>>,
    right: Option<Box<ListNode>>,
) -> &'a mut Option<Box<ListNode>> {
    let mut left_list = left;
    let mut right_list = right;
    let orig_head = head.take();
    let mut new_head = head;
    loop {
        match (left_list.take(), right_list.take()) {
            (Some(mut left_node), Some(mut right_node)) => {
                if left_node.val <= right_node.val {
                    left_node.as_mut().next.take().map(|n| left_list.replace(n));
                    right_list.replace(right_node);
                    new_head.replace(left_node);
                    new_head = &mut new_head.as_mut().unwrap().next;
                } else {
                    right_node
                        .as_mut()
                        .next
                        .take()
                        .map(|n| right_list.replace(n));
                    left_list.replace(left_node);
                    new_head.replace(right_node);
                    new_head = &mut new_head.as_mut().unwrap().next;
                }
            }
            (Some(left_node), None) => {
                new_head.replace(left_node);
                while let Some(ref mut node) = new_head {
                    new_head = &mut node.next;
                }
                orig_head.map(|oh| new_head.replace(oh));
                return new_head;
            }
            (None, Some(right_node)) => {
                new_head.replace(right_node);
                while let Some(ref mut node) = new_head {
                    new_head = &mut node.next;
                }
                orig_head.map(|oh| new_head.replace(oh));
                return new_head;
            }
            (None, None) => {
                orig_head.map(|oh| new_head.replace(oh));
                return new_head;
            }
        }
    }
}
```
最后再次推荐一下这篇教程[Learn Rust With Entirely Too Many Linked Lists]。

这篇教程结合实际代码，将ownership，lifetime和borrow check解释得十分清楚。行文也十分生动有趣。作者还写了[The Rustonomicon]，功力深厚是毫无疑问的。

[Learn Rust With Entirely Too Many Linked Lists]: https://rust-unofficial.github.io/too-many-lists/
[Sort List]: https://leetcode.com/problems/sort-list/
[The Rustonomicon]: https://doc.rust-lang.org/nightly/nomicon/