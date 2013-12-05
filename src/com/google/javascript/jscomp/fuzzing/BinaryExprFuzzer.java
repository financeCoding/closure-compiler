/*
 * Copyright 2013 The Closure Compiler Authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.google.javascript.jscomp.fuzzing;

import com.google.common.base.CaseFormat;
import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;

import org.json.JSONObject;

import java.util.Random;

/**
 * UNDER DEVELOPMENT. DO NOT USE!
 */
public class BinaryExprFuzzer extends Dispatcher {

  public BinaryExprFuzzer(Random random, ScopeManager scopeManager,
      JSONObject config, StringNumberGenerator snGenerator) {
    super(random, scopeManager, config, snGenerator);
  }

  /* (non-Javadoc)
   * @see com.google.javascript.jscomp.fuzzing.Dispatcher#initCandidates()
   */
  @Override
  protected void initCandidates() {
    Operator[] operators = Operator.values();
    candidates = new BinaryExprGenerator[operators.length];
    for (int i = 0; i < operators.length; i++) {
      candidates[i] = new BinaryExprGenerator(
          random, scopeManager, config, snGenerator, operators[i]);
    }
  }

  private class BinaryExprGenerator extends AbstractFuzzer {
    private Operator operator;
    private AbstractFuzzer left, right;
    private String configName;
    BinaryExprGenerator(Random random, ScopeManager scopeManager,
      JSONObject config, StringNumberGenerator snGenerator, Operator operator) {
      super(random, scopeManager, config, snGenerator);
      this.configName = CaseFormat.UPPER_UNDERSCORE.to(
          CaseFormat.LOWER_CAMEL, operator.name());
      this.operator = operator;
    }

    /* (non-Javadoc)
     * @see com.google.javascript.jscomp.fuzzing.AbstractFuzzer#generate(int)
     */
    @Override
    protected Node generate(int budget) {
      Node[] operands = distribute(
          budget - 1, new AbstractFuzzer[] {getLeft(),  getRight()});

      return new Node(operator.nodeType, operands);
    }

    private AbstractFuzzer getLeft() {
      if (left == null) {
        left = operator.hasSideEffect() ?
          new AssignableExprFuzzer(random, scopeManager, config, snGenerator) :
          new ExpressionFuzzer(random, scopeManager, config, snGenerator);
      }
      return left;
    }



    private AbstractFuzzer getRight() {
      if (right == null) {
        right = new ExpressionFuzzer(random, scopeManager, config, snGenerator);
      }
      return right;
    }

    /* (non-Javadoc)
     * @see com.google.javascript.jscomp.fuzzing.AbstractFuzzer#isEnough(int)
     */
    @Override
    protected boolean isEnough(int budget) {
      if (budget < 1) {
        return false;
      } else {
        // This is only an optimistic estimation
        return getLeft().isEnough(budget - 2) &&
            getRight().isEnough(budget - 2);
      }
    }

    /* (non-Javadoc)
     * @see com.google.javascript.jscomp.fuzzing.AbstractFuzzer#getConfigName()
     */
    @Override
    protected String getConfigName() {
      return configName;
    }

  }

  private enum Operator {
    MUL(Token.MUL),
    DIV(Token.DIV),
    MOD(Token.MOD),
    ADD(Token.ADD),
    SUB(Token.SUB),
    LSH(Token.LSH),
    RSH(Token.RSH),
    URSH(Token.URSH),
    LT(Token.LT),
    GT(Token.GT),
    LE(Token.LE),
    GE(Token.GE),
    INSTANCEOF(Token.INSTANCEOF),
    IN(Token.IN),
    EQ(Token.EQ),
    NE(Token.NE),
    SHEQ(Token.SHEQ),
    SHNE(Token.SHNE),
    BIT_AND(Token.BITAND),
    BIT_XOR(Token.BITXOR),
    BIT_OR(Token.BITOR),
    AND(Token.AND),
    OR(Token.OR),
    ASSIGN(Token.ASSIGN),
    ASSIGN_MUL(Token.ASSIGN_MUL),
    ASSIGN_DIV(Token.ASSIGN_DIV),
    ASSIGN_MOD(Token.ASSIGN_MOD),
    ASSIGN_ADD(Token.ASSIGN_ADD),
    ASSIGN_SUB(Token.ASSIGN_SUB),
    ASSIGN_LSH(Token.ASSIGN_LSH),
    ASSIGN_RSH(Token.ASSIGN_RSH),
    ASSIGN_URSH(Token.ASSIGN_URSH),
    ASSIGN_BIT_AND(Token.ASSIGN_BITAND),
    ASSIGN_BIT_XOR(Token.ASSIGN_BITXOR),
    ASSIGN_BIT_OR(Token.ASSIGN_BITOR);

    int nodeType;
    Operator(int nodeType) {
      this.nodeType = nodeType;
    }

    boolean hasSideEffect() {
      return this.name().startsWith("ASSIGN");
    }
  }

  /* (non-Javadoc)
   * @see com.google.javascript.jscomp.fuzzing.AbstractFuzzer#getConfigName()
   */
  @Override
  protected String getConfigName() {
    return "binaryExpr";
  }

}