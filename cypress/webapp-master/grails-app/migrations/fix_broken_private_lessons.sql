insert into booking_trainer
select b2.id as booking_id, b_groups.trainer_id as trainer_id from
(select bg.id, min(bt.trainer_id) as trainer_id  from booking b
inner join booking_group bg on b.group_id = bg.id
left join booking_trainer bt on b.id = bt.booking_id
where bg.type='PRIVATE_LESSON'
group by bg.id) b_groups
join booking b2 on b2.group_id=b_groups.id
left join booking_trainer t on b2.id = t.booking_id
where t.trainer_id is null and b_groups.trainer_id is not null;
