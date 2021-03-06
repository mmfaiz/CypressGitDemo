ALTER TABLE `booking` DROP FOREIGN KEY FK3DB0859A4738097;
ALTER TABLE `season_deviation_slot` DROP FOREIGN KEY FK829757D8A4738097;
ALTER TABLE `slot_payment` DROP FOREIGN KEY FKBDCEE4C5E133D447;
ALTER TABLE `slot_redeem` DROP FOREIGN KEY fk_sr_slot_id;

ALTER DATABASE matchi CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

ALTER TABLE UserConnection ROW_FORMAT=DYNAMIC;
ALTER TABLE UserConnection CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
ALTER TABLE abstract_price_condition ROW_FORMAT=DYNAMIC;
ALTER TABLE abstract_price_condition CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
ALTER TABLE activity ROW_FORMAT=DYNAMIC;
ALTER TABLE activity CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
ALTER TABLE activity_occasion ROW_FORMAT=DYNAMIC;
ALTER TABLE activity_occasion CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
ALTER TABLE activity_occasion_booking ROW_FORMAT=DYNAMIC;
ALTER TABLE activity_occasion_booking CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
ALTER TABLE activity_occasion_participants ROW_FORMAT=DYNAMIC;
ALTER TABLE activity_occasion_participants CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
ALTER TABLE activity_occasion_trainer ROW_FORMAT=DYNAMIC;
ALTER TABLE activity_occasion_trainer CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
ALTER TABLE activity_slot_condition_class_activity ROW_FORMAT=DYNAMIC;
ALTER TABLE activity_slot_condition_class_activity CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
ALTER TABLE async_mail_attachment ROW_FORMAT=DYNAMIC;
ALTER TABLE async_mail_attachment CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
ALTER TABLE async_mail_bcc ROW_FORMAT=DYNAMIC;
ALTER TABLE async_mail_bcc CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
ALTER TABLE async_mail_cc ROW_FORMAT=DYNAMIC;
ALTER TABLE async_mail_cc CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
ALTER TABLE async_mail_header ROW_FORMAT=DYNAMIC;
ALTER TABLE async_mail_header CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
ALTER TABLE async_mail_mess ROW_FORMAT=DYNAMIC;
ALTER TABLE async_mail_mess CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
ALTER TABLE async_mail_to ROW_FORMAT=DYNAMIC;
ALTER TABLE async_mail_to CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
ALTER TABLE availability ROW_FORMAT=DYNAMIC;
ALTER TABLE availability CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
ALTER TABLE booking ROW_FORMAT=DYNAMIC;
ALTER TABLE booking CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
ALTER TABLE booking_export ROW_FORMAT=DYNAMIC;
ALTER TABLE booking_export CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
ALTER TABLE booking_group ROW_FORMAT=DYNAMIC;
ALTER TABLE booking_group CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
ALTER TABLE boxnet_transaction ROW_FORMAT=DYNAMIC;
ALTER TABLE boxnet_transaction CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
ALTER TABLE cash_register_transaction ROW_FORMAT=DYNAMIC;
ALTER TABLE cash_register_transaction CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
ALTER TABLE code_request ROW_FORMAT=DYNAMIC;
ALTER TABLE code_request CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
ALTER TABLE contact_me ROW_FORMAT=DYNAMIC;
ALTER TABLE contact_me CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
ALTER TABLE coupon ROW_FORMAT=DYNAMIC;
ALTER TABLE coupon CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
ALTER TABLE coupon_condition_group ROW_FORMAT=DYNAMIC;
ALTER TABLE coupon_condition_group CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
ALTER TABLE coupon_condition_groups_slot_conditions_sets ROW_FORMAT=DYNAMIC;
ALTER TABLE coupon_condition_groups_slot_conditions_sets CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
ALTER TABLE coupon_price ROW_FORMAT=DYNAMIC;
ALTER TABLE coupon_price CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
ALTER TABLE course_activity_trainer ROW_FORMAT=DYNAMIC;
ALTER TABLE course_activity_trainer CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
ALTER TABLE court ROW_FORMAT=DYNAMIC;
ALTER TABLE court CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
ALTER TABLE court_facility_access_codes ROW_FORMAT=DYNAMIC;
ALTER TABLE court_facility_access_codes CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
ALTER TABLE court_price_condition_courts ROW_FORMAT=DYNAMIC;
ALTER TABLE court_price_condition_courts CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
ALTER TABLE court_slot_condition_court ROW_FORMAT=DYNAMIC;
ALTER TABLE court_slot_condition_court CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
ALTER TABLE customer ROW_FORMAT=DYNAMIC;
ALTER TABLE customer CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
ALTER TABLE customer_coupon ROW_FORMAT=DYNAMIC;
ALTER TABLE customer_coupon CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
ALTER TABLE customer_coupon_ticket ROW_FORMAT=DYNAMIC;
ALTER TABLE customer_coupon_ticket CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
ALTER TABLE customer_group ROW_FORMAT=DYNAMIC;
ALTER TABLE customer_group CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
ALTER TABLE customer_group_price_condition_facility_group ROW_FORMAT=DYNAMIC;
ALTER TABLE customer_group_price_condition_facility_group CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
ALTER TABLE device ROW_FORMAT=DYNAMIC;
ALTER TABLE device CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
ALTER TABLE external_synchronization_entity ROW_FORMAT=DYNAMIC;
ALTER TABLE external_synchronization_entity CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
ALTER TABLE facility ROW_FORMAT=DYNAMIC;
ALTER TABLE facility CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
ALTER TABLE facility_access_code ROW_FORMAT=DYNAMIC;
ALTER TABLE facility_access_code CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
ALTER TABLE facility_availability ROW_FORMAT=DYNAMIC;
ALTER TABLE facility_availability CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
ALTER TABLE facility_booking_cancel_rule ROW_FORMAT=DYNAMIC;
ALTER TABLE facility_booking_cancel_rule CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
ALTER TABLE facility_contract ROW_FORMAT=DYNAMIC;
ALTER TABLE facility_contract CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
ALTER TABLE facility_contract_item ROW_FORMAT=DYNAMIC;
ALTER TABLE facility_contract_item CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
ALTER TABLE facility_contract_item_charge_months ROW_FORMAT=DYNAMIC;
ALTER TABLE facility_contract_item_charge_months CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
ALTER TABLE facility_form_templates ROW_FORMAT=DYNAMIC;
ALTER TABLE facility_form_templates CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
ALTER TABLE facility_group ROW_FORMAT=DYNAMIC;
ALTER TABLE facility_group CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
ALTER TABLE facility_message ROW_FORMAT=DYNAMIC;
ALTER TABLE facility_message CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
ALTER TABLE facility_property ROW_FORMAT=DYNAMIC;
ALTER TABLE facility_property CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
ALTER TABLE facility_sport ROW_FORMAT=DYNAMIC;
ALTER TABLE facility_sport CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
ALTER TABLE facility_user ROW_FORMAT=DYNAMIC;
ALTER TABLE facility_user CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
ALTER TABLE facility_user_role ROW_FORMAT=DYNAMIC;
ALTER TABLE facility_user_role CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
ALTER TABLE form ROW_FORMAT=DYNAMIC;
ALTER TABLE form CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
ALTER TABLE form_field ROW_FORMAT=DYNAMIC;
ALTER TABLE form_field CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
ALTER TABLE form_field_value ROW_FORMAT=DYNAMIC;
ALTER TABLE form_field_value CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
ALTER TABLE form_template ROW_FORMAT=DYNAMIC;
ALTER TABLE form_template CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
ALTER TABLE global_notification ROW_FORMAT=DYNAMIC;
ALTER TABLE global_notification CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
ALTER TABLE invoice ROW_FORMAT=DYNAMIC;
ALTER TABLE invoice CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
ALTER TABLE invoice_payment ROW_FORMAT=DYNAMIC;
ALTER TABLE invoice_payment CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
ALTER TABLE invoice_row ROW_FORMAT=DYNAMIC;
ALTER TABLE invoice_row CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
ALTER TABLE iosync ROW_FORMAT=DYNAMIC;
ALTER TABLE iosync CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
ALTER TABLE member_type_price_condition_membership_type ROW_FORMAT=DYNAMIC;
ALTER TABLE member_type_price_condition_membership_type CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
ALTER TABLE membership ROW_FORMAT=DYNAMIC;
ALTER TABLE membership CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
ALTER TABLE membership_family ROW_FORMAT=DYNAMIC;
ALTER TABLE membership_family CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
ALTER TABLE membership_payment_history ROW_FORMAT=DYNAMIC;
ALTER TABLE membership_payment_history CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
ALTER TABLE membership_type ROW_FORMAT=DYNAMIC;
ALTER TABLE membership_type CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
ALTER TABLE mfile ROW_FORMAT=DYNAMIC;
ALTER TABLE mfile CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
ALTER TABLE municipality ROW_FORMAT=DYNAMIC;
ALTER TABLE municipality CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
ALTER TABLE `order` ROW_FORMAT=DYNAMIC;
ALTER TABLE `order`CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
ALTER TABLE order_metadata ROW_FORMAT=DYNAMIC;
ALTER TABLE order_metadata CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
ALTER TABLE order_order_payments ROW_FORMAT=DYNAMIC;
ALTER TABLE order_order_payments CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
ALTER TABLE order_payment ROW_FORMAT=DYNAMIC;
ALTER TABLE order_payment CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
ALTER TABLE order_refund ROW_FORMAT=DYNAMIC;
ALTER TABLE order_refund CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
ALTER TABLE organization ROW_FORMAT=DYNAMIC;
ALTER TABLE organization CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
ALTER TABLE participant ROW_FORMAT=DYNAMIC;
ALTER TABLE participant CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
ALTER TABLE participation ROW_FORMAT=DYNAMIC;
ALTER TABLE participation CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
ALTER TABLE payment ROW_FORMAT=DYNAMIC;
ALTER TABLE payment CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
ALTER TABLE payment_info ROW_FORMAT=DYNAMIC;
ALTER TABLE payment_info CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
ALTER TABLE payment_order ROW_FORMAT=DYNAMIC;
ALTER TABLE payment_order CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
ALTER TABLE payment_order_order_parameters ROW_FORMAT=DYNAMIC;
ALTER TABLE payment_order_order_parameters CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
ALTER TABLE payment_order_parameters ROW_FORMAT=DYNAMIC;
ALTER TABLE payment_order_parameters CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
ALTER TABLE payment_request ROW_FORMAT=DYNAMIC;
ALTER TABLE payment_request CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
ALTER TABLE payment_transaction ROW_FORMAT=DYNAMIC;
ALTER TABLE payment_transaction CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
ALTER TABLE player ROW_FORMAT=DYNAMIC;
ALTER TABLE player CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
ALTER TABLE price ROW_FORMAT=DYNAMIC;
ALTER TABLE price CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
ALTER TABLE price_list ROW_FORMAT=DYNAMIC;
ALTER TABLE price_list CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
ALTER TABLE price_list_condition_category ROW_FORMAT=DYNAMIC;
ALTER TABLE price_list_condition_category CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
ALTER TABLE price_list_customer_category ROW_FORMAT=DYNAMIC;
ALTER TABLE price_list_customer_category CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
ALTER TABLE redeem_strategy ROW_FORMAT=DYNAMIC;
ALTER TABLE redeem_strategy CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
ALTER TABLE region ROW_FORMAT=DYNAMIC;
ALTER TABLE region CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
ALTER TABLE role ROW_FORMAT=DYNAMIC;
ALTER TABLE role CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
ALTER TABLE scheduled_task ROW_FORMAT=DYNAMIC;
ALTER TABLE scheduled_task CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
ALTER TABLE season ROW_FORMAT=DYNAMIC;
ALTER TABLE season CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
ALTER TABLE season_court_opening_hours ROW_FORMAT=DYNAMIC;
ALTER TABLE season_court_opening_hours CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
ALTER TABLE season_deviation ROW_FORMAT=DYNAMIC;
ALTER TABLE season_deviation CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
ALTER TABLE season_deviation_slot ROW_FORMAT=DYNAMIC;
ALTER TABLE season_deviation_slot CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
ALTER TABLE slot ROW_FORMAT=DYNAMIC;
ALTER TABLE slot CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
ALTER TABLE slot_condition ROW_FORMAT=DYNAMIC;
ALTER TABLE slot_condition CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
ALTER TABLE slot_condition_set ROW_FORMAT=DYNAMIC;
ALTER TABLE slot_condition_set CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
ALTER TABLE slot_condition_set_slot_condition ROW_FORMAT=DYNAMIC;
ALTER TABLE slot_condition_set_slot_condition CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
ALTER TABLE slot_payment ROW_FORMAT=DYNAMIC;
ALTER TABLE slot_payment CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
ALTER TABLE slot_redeem ROW_FORMAT=DYNAMIC;
ALTER TABLE slot_redeem CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
ALTER TABLE slot_watch ROW_FORMAT=DYNAMIC;
ALTER TABLE slot_watch CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
ALTER TABLE slot_watch_event ROW_FORMAT=DYNAMIC;
ALTER TABLE slot_watch_event CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
ALTER TABLE sport ROW_FORMAT=DYNAMIC;
ALTER TABLE sport CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
ALTER TABLE sport_attribute ROW_FORMAT=DYNAMIC;
ALTER TABLE sport_attribute CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
ALTER TABLE sport_profile ROW_FORMAT=DYNAMIC;
ALTER TABLE sport_profile CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
ALTER TABLE sport_profile_attribute ROW_FORMAT=DYNAMIC;
ALTER TABLE sport_profile_attribute CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
ALTER TABLE sport_profile_mindset ROW_FORMAT=DYNAMIC;
ALTER TABLE sport_profile_mindset CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
ALTER TABLE sport_profile_mindsets ROW_FORMAT=DYNAMIC;
ALTER TABLE sport_profile_mindsets CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
ALTER TABLE submission ROW_FORMAT=DYNAMIC;
ALTER TABLE submission CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
ALTER TABLE submission_value ROW_FORMAT=DYNAMIC;
ALTER TABLE submission_value CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
ALTER TABLE subscription ROW_FORMAT=DYNAMIC;
ALTER TABLE subscription CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
ALTER TABLE subscription_redeem ROW_FORMAT=DYNAMIC;
ALTER TABLE subscription_redeem CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
ALTER TABLE ticket ROW_FORMAT=DYNAMIC;
ALTER TABLE ticket CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
ALTER TABLE token ROW_FORMAT=DYNAMIC;
ALTER TABLE token CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
ALTER TABLE trainer ROW_FORMAT=DYNAMIC;
ALTER TABLE trainer CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
ALTER TABLE training_court ROW_FORMAT=DYNAMIC;
ALTER TABLE training_court CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
ALTER TABLE `user` ROW_FORMAT=DYNAMIC;
ALTER TABLE `user` CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
ALTER TABLE user_availability ROW_FORMAT=DYNAMIC;
ALTER TABLE user_availability CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
ALTER TABLE user_favorite ROW_FORMAT=DYNAMIC;
ALTER TABLE user_favorite CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
ALTER TABLE user_message ROW_FORMAT=DYNAMIC;
ALTER TABLE user_message CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
ALTER TABLE user_role ROW_FORMAT=DYNAMIC;
ALTER TABLE user_role CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
ALTER TABLE weekday_slot_condition_weekdays ROW_FORMAT=DYNAMIC;
ALTER TABLE weekday_slot_condition_weekdays CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

ALTER TABLE `booking` ADD CONSTRAINT FK3DB0859A4738097 FOREIGN KEY (slot_id) REFERENCES `slot` (`id`);
ALTER TABLE `season_deviation_slot` ADD CONSTRAINT FK829757D8A4738097 FOREIGN KEY (slot_id) REFERENCES `slot` (`id`);
ALTER TABLE `slot_payment` ADD CONSTRAINT FKBDCEE4C5E133D447 FOREIGN KEY (slot_payments_id) REFERENCES `slot` (`id`);
ALTER TABLE `slot_redeem` ADD CONSTRAINT fk_sr_slot_id FOREIGN KEY (slot_id) REFERENCES `slot` (`id`);