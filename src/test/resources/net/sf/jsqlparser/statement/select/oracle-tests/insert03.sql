---
-- #%L
-- JSQLParser library
-- %%
-- Copyright (C) 2004 - 2019 JSQLParser
-- %%
-- Dual licensed under GNU LGPL 2.1 or Apache License 2.0
-- #L%
---
-- insert when
insert 
when (deptno=10) then
  into emp_10 (empno,ename,job,mgr,sal,deptno)
  values (empno,ename,job,mgr,sal,deptno)
when (deptno=20) then
  into emp_20 (empno,ename,job,mgr,sal,deptno)
  values (empno,ename,job,mgr,sal,deptno)
when (deptno=30) then
  into emp_30 (empno,ename,job,mgr,sal,deptno)
  values (empno,ename,job,mgr,sal,deptno)
else
  into leftover (empno,ename,job,mgr,sal,deptno)
  values (empno,ename,job,mgr,sal,deptno)
select * from emp

--@FAILURE: Encountered unexpected token: "when" "WHEN" recorded first on Aug 3, 2021, 7:20:08 AM
--@FAILURE: Encountered unexpected token: "insert" "INSERT" recorded first on 11 Jan 2023, 21:07:10
--@FAILURE: Encountered: <K_INSERT> / "insert", at line 11, column 1, in lexical state DEFAULT. recorded first on 15 May 2025, 16:24:08