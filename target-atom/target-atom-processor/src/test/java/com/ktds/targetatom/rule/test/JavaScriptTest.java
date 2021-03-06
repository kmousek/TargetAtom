package com.ktds.targetatom.rule.test;
/*package com.ktds.targetatom.rule.test;

import java.util.List;
import java.util.Map;

import org.apache.camel.EndpointInject;
import org.apache.camel.Produce;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.model.dataformat.FlatpackDataFormat;
import org.apache.camel.test.junit4.CamelTestSupport;
import org.json.JSONObject;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.PropertySource;

import com.ktds.targetatom.bean.CdrProcessor;
import com.ktds.targetatom.drools.ValidationResult;
import com.ktds.targetatom.util.CdrUtils;

import junit.framework.Assert;

@PropertySource(value = {"classpath:application.properties", "classpath:camel-properties.properties"})
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class JavaScriptTest extends CamelTestSupport {
	protected final Logger log = LoggerFactory.getLogger(getClass());	
	private static final String URI_DIRECT_UNMARSHAL = "direct:unmarshal";
	private static final String URI_UNMARSHAL_RESULT = "mock:unmarshal_result";
	private static final String URI_DIRECT_MARSHAL = "direct:marshal";
	private static final String URI_MARSHAL_RESULT = "mock:marshal_result";

	private static final String TEST_HEADER = "        SLTEB   ST0SCP01KTFSLHC1                06062019050223533452184    KTFHAD10                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                            1";
	private static final String TEST_RECORD = "01006010981479614500867801107051.103.64.254   044TES lte.ktfwing.com                                                                                     4110.70.226.1   110.70.236.1   036  020045008 08-79060003E8000003E800        1872414641                                                                                                54F08002A2D003  54F08002A2D003        201905022214152019050223533400000000002737940000000007408640000059594993  CR1                                                             201905022353272019050223533400000000000069980000000000633195      00000006          0000000200                    08-79060003E8000003E800                                                                                                                                                      S"
																					+ "0100601098947901450087460173966               044TES lte.ktfwing.com                                                                                     4110.70.130.1   110.70.234.1   236  020045008 08-79060003E8000003E800        1372140229                                                                                                54F0800106DB02  54F0800106DB02        201905022353312019050223571300000000000053260000000000001659000002224993  CR1                                                             201905022353312019050223571300000000000053260000000000001659      00000222          0000000002                    08-79060003E8000003E800        2001:e60:1028:fff6:0:0:0:0                                                                                                                    2";
	private static final String TEST_FOOTER = " SLTEB_FW4GCLTE_ID0606_T20190502235802.DAT      20190502235834571830005000000500000008000004001600KTFTAL10                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                     3";

	@Produce(uri = URI_DIRECT_UNMARSHAL)
	private ProducerTemplate unmarshalTemplate;

	@EndpointInject(uri = URI_UNMARSHAL_RESULT)
	private MockEndpoint unmarshalResult;

	@Produce(uri = URI_DIRECT_MARSHAL)
	private ProducerTemplate marshalTemplate;

	@EndpointInject(uri = URI_MARSHAL_RESULT)
	private MockEndpoint marshalResult;

	private static List<Map<String, Object>> listUnmarshal;
	
	@Test
	public void test1_UnMarshal() throws Exception {
		StringBuffer buff = new StringBuffer();
		buff.append(TEST_HEADER).append(TEST_RECORD).append(TEST_FOOTER);

		unmarshalResult.expectedMessageCount(1);

		unmarshalTemplate.sendBody(URI_DIRECT_UNMARSHAL, buff.toString());
		assertMockEndpointsSatisfied();

		ValidationResult result = unmarshalResult.getExchanges().get(0).getIn().getBody(ValidationResult.class);
		log.info("output count={}", result.getOutputList().size());
		
		// Store unmarshal result to test2_marshal
		listUnmarshal = result.getErrorList();

		log.info("error count={}", result.getErrorList().size());
		log.info("output count={}", result.getOutputList().size());
		Assert.assertEquals(2, result.getOutputList().size());
		Assert.assertEquals(0, result.getErrorList().size());
	}

//	@Test
//	public void test2_Marshal() throws Exception {
//		log.info("source={}", listUnmarshal);
//		marshalTemplate.sendBody(URI_DIRECT_MARSHAL, listUnmarshal);
//		assertMockEndpointsSatisfied();
//		
//		log.info("count={}", marshalResult.getReceivedCounter());
//		log.info("exchange={}", marshalResult.getReceivedExchanges());
//		String result = marshalResult.getExchanges().get(0).getIn().getBody(String.class);
//		log.info("result=[{}]", result);
//		log.info("expect length={}", TEST_RECORD.length());
//		log.info("Result length={}", result.length());
//		assertEquals(TEST_RECORD, result);
//	}

	@Override
	protected RouteBuilder createRouteBuilder() throws Exception {
		RouteBuilder routeBuilder = new RouteBuilder() {

			@Override
			public void configure() throws Exception {
				FlatpackDataFormat inFormat = new FlatpackDataFormat();
				inFormat.setDefinition("spring/dataformat/volte/VOLTE_INPUT_FORMAT.xml");
				inFormat.setFixed(true);

				FlatpackDataFormat outFormat = new FlatpackDataFormat();
				outFormat.setDefinition("spring/dataformat/volte/VOLTE_OUTPUT_FORMAT.xml");
				outFormat.setFixed(true);
				
				FlatpackDataFormat errFormat = new FlatpackDataFormat();
				errFormat.setDefinition("spring/dataformat/volte/VOLTE_INPUT_FORMAT.xml");
				errFormat.setFixed(true);
				
				
				from(URI_DIRECT_UNMARSHAL)
					.setHeader("cdrFormat", constant("volte"))
					.setHeader("cdrHeaderLength", constant("800"))
					.setHeader("cdrRecordLength", constant("800"))
					.setHeader("skipCdrHeader", constant(true))
					.transform().method(CdrUtils.class, "addNewLine")
					.unmarshal(inFormat)
					.to("language:javascript:classpath:spring/rules/volte/volte_test.js")
					.log("Body :::::: ${body}")
					.to(URI_UNMARSHAL_RESULT);
				
				from(URI_DIRECT_MARSHAL)
					.marshal(outFormat)
					.convertBodyTo(String.class)
					.transform().method(CdrUtils.class, "removeNewLine")
					.to(URI_MARSHAL_RESULT);
			}
		};

		return routeBuilder;
	}
}
*/