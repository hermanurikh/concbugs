package com.qbutton.concbugs.algorythm.processor;

import com.qbutton.concbugs.algorythm.dto.State;
import com.qbutton.concbugs.algorythm.dto.statement.Statement;

abstract class AbstractStatementProcessor<T extends Statement> {

    abstract State process(T statement, State originalState);
}
