package com.carota.fota_build

import com.carota.hmi.CarOtaHmi
import com.carota.hmi.type.HmiTaskType
import com.carota.hmi.type.UpgradeType

object PolicyFactory {

    /**
     * normal upgrade
     */
    private val defaultPolicy: CarOtaHmi.Policy = CarOtaHmi.Policy().apply {
        this.addHmiTask(HmiTaskType.check)
        this.addHmiTask(HmiTaskType.wait_user_run_next)
        this.addHmiTask(HmiTaskType.download)
        this.addHmiTask(HmiTaskType.wait_user_run_next)
        this.addHmiTask(HmiTaskType.condition)
        this.addHmiTask(HmiTaskType.wait_user_run_next)
        this.addHmiTask(HmiTaskType.enter_ota)
        this.addHmiTask(HmiTaskType.wait_user_run_next)
        this.addHmiTask(HmiTaskType.task_timeout_verify)
        this.addHmiTask(HmiTaskType.wait_user_run_next)
        this.addHmiTask(HmiTaskType.install)
        this.addHmiTask(HmiTaskType.wait_user_run_next)
        this.addHmiTask(HmiTaskType.exit_ota)
    }

    /**
     * factory upgrade
     */
    private val factoryPolicy: CarOtaHmi.Policy = CarOtaHmi.Policy().apply {
        this.addHmiTask(HmiTaskType.check)
        this.addHmiTask(HmiTaskType.download)
        this.addHmiTask(HmiTaskType.condition)
        this.addHmiTask(HmiTaskType.enter_ota)
        this.addHmiTask(HmiTaskType.task_timeout_verify)
        this.addHmiTask(HmiTaskType.install)
        this.addHmiTask(HmiTaskType.exit_ota)
    }

    val policyMap = hashMapOf(
        UpgradeType.DEFULT to defaultPolicy,
        UpgradeType.FACTORY to factoryPolicy
        )
}