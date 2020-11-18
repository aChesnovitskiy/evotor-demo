package com.example.evotordemo

import android.os.RemoteException
import ru.evotor.framework.component.PaymentPerformer
import ru.evotor.framework.component.PaymentPerformerApi
import ru.evotor.framework.core.IntegrationService
import ru.evotor.framework.core.action.event.receipt.changes.position.SetExtra
import ru.evotor.framework.core.action.event.receipt.payment.combined.PaymentDelegatorEventProcessor
import ru.evotor.framework.core.action.event.receipt.payment.combined.event.PaymentDelegatorEvent
import ru.evotor.framework.core.action.event.receipt.payment.combined.result.PaymentDelegatorSelectedEventResult
import ru.evotor.framework.core.action.processor.ActionProcessor
import ru.evotor.framework.payment.PaymentPurpose
import ru.evotor.framework.payment.PaymentSystem
import ru.evotor.framework.payment.PaymentType
import java.math.BigDecimal

/*
Also look SplitService here: https://github.com/evotor/evotor-api-example
And here: https://developer.evotor.ru/docs/doc_java_receipt_division.html
There are similar things with CombinedPaymentService. And also there is creation of the PaymentPerformer and the PaymentPurpose.
 */

class CombinedPaymentService : IntegrationService() {

    override fun createProcessors(): MutableMap<String, ActionProcessor> {
        val paymentPerformers: List<PaymentPerformer> =
            PaymentPerformerApi.getAllPaymentPerformers(packageManager)

        val chosenPaymentPerformer: PaymentPerformer =
            paymentPerformers[0] // or somehow differently choose PaymentPerformer

        // Or, maybe, create new PaymentPerformer. Copy from SplitService in the evotor-api-example
        val createdPaymentPerformer: PaymentPerformer = PaymentPerformer(
            //Объект с описанием платёжной системы, которое использует приложение, исполняющее платёж.
            paymentSystem = PaymentSystem(
                PaymentType.ELECTRON,
                "Some description",
                "Payment system ID"
            ),
            //Пакет, в котором расположен компонент, исполняющий платёж.
            packageName = "com.example.evotordemo",
            //Название компонента, исполняющего платёж.
            componentName = "ComponentName",
            //Идентификатор уникальный идентификатор приложения, исполняющего платёж.
            appUuid = "App identifier",
            //Название приложения, исполняющего платёж
            appName = "Evotor demo"
        )

        // Copy from SplitService in the evotor-api-example
        val paymentPurpose = PaymentPurpose(
            //Идентификатор платежа.
            "Payment identifier",
            //Идентификатор платёжной системы. Устаревший параметр.
            "Deprecated PaymentSystemId or Null",
            //Установленное на смарт-терминале приложение или его компонент, выполняющее платёж.
            chosenPaymentPerformer, // or createdPaymentPerformer
            //Сумма платежа.
            BigDecimal(50000),
            "Payment account identifier",
            //Сообщение для пользователя.
            "Your payment has proceeded successfully."
        )

        val paymentDelegatorEventProcessor: PaymentDelegatorEventProcessor =
            object : PaymentDelegatorEventProcessor() {

                override fun call(
                    action: String,
                    event: PaymentDelegatorEvent,
                    callback: Callback
                ) {
                    try {
                        callback.onResult(
                            PaymentDelegatorSelectedEventResult(
                                paymentPurpose = paymentPurpose,
                                extra = SetExtra(null) // Доп. поля в чеке в виде валидного JSON-объекта. Не знаю, нужно ли оно нам и в каком виде.
                            )
                        )

                        /*
                        But also we can put to the "onResult" PaymentDelegatorCanceledEventResult or
                        PaymentDelegatorCanceledAllEventResult instead of PaymentDelegatorSelectedEventResult.
                        Somehow we must define which of the three to put.
                         */
                    } catch (exception: RemoteException) {
                        exception.printStackTrace()
                    }
                }

            }

        return mutableMapOf<String, ActionProcessor>().apply {
            put(PaymentDelegatorEvent.NAME_ACTION, paymentDelegatorEventProcessor)
        }
    }
}