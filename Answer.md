## 주관식 문제
>1. 테스트코드 작성시 setup해야 할 데이터가 대용량이라면 어떤 방식으로 테스트코드를 작성해야할까?
>>우선 setup할 데이터들을 보고 테스트에 사용할 임시테이블을 생성합니다.   
>>해당 데이터를 엑셀에서 데이터와 중복되지 않을 구분자를 가지고 간단한 txt파일로 변환합니다.   
>>이후에 bcp 명령어를 사용하여 임시테이블에 넣어두고 테스트를 진행합니다.


>4. 외부 의존성이 높은 서비스를 만들때 고려해야할 사항이 무엇인가
>>라이브러리의 의존성이 높은 경우에는 버전에 따라 다른 라이브러리와 정상적으로 호환이 되는지에 대한 확인도 필요하겠지만   
>>보안에 있어서 문제가 될 수도 있는 경우가 있기에 관련 라이브러리에 대한 뉴스도 살펴보아야 할것 같습니다.   
>>그래야 최근에 log4j 보안 문제처럼 모르고 사용하더라도 해당 이슈에 대해 빠른 대처가 가능하다고 생각합니다.
>>또한 외부 api를 호출하여 서비스하는 경우에는 통신 장애로 인한 서비스 불가능 문제가 발생할 수 있기에 health체크 로직을 추가하여 모니터링의 필요성이 있습니다.


>5. 일정이 촉박한 프로젝트에서 평소 습관대로 개발을 진행할지? 아니면 회사의 코드 컨벤션에 맞춰 개발할지?
>>코드명이나 네이밍 규칙 같은 가이드가 있다면 그정도는 회사의 가이드에 맞게 하는 것이 맞다고 생각합니다.   
>>하지만 그만큼 단순하지 않은 부분이라면 일정이 촉박하기에 우선은 평소 습관대로 개발을 하고   
>>나중에 유지 보수를 하면서 조금씩 회사의 컨벤션에 맞게 수정해야 할것 같습니다.   
>>가이드 된 대로 정확하게 개발하는 것도 중요하지만, 제가 맡은 부분에 있어서 개발이 완성되지 않으면   
>>같은 팀원들이 잘 개발한 프로그램까지도 같이 필요 없어져버릴수 있기 때문입니다.
