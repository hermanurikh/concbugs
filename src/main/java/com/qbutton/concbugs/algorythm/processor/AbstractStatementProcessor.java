package com.qbutton.concbugs.algorythm.processor;

import com.qbutton.concbugs.algorythm.dto.statement.Statement;

abstract class AbstractStatementProcessor<T extends Statement> {

    abstract void process(T statement);
}
